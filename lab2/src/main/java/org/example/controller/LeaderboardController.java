package org.example.controller;

import org.example.model.ScoreRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardController.class);
    private static final String LEADERBOARD_KEY = "leaderboard";

    private final StringRedisTemplate redisTemplate;

    public LeaderboardController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/score")
    public ResponseEntity<?> addScore(@RequestBody ScoreRequest req) {
        try {
            Double newScore = redisTemplate.opsForZSet()
                    .incrementScore(LEADERBOARD_KEY, req.player(), req.score());
            return ResponseEntity.ok(Map.of(
                    "player", req.player(),
                    "score", newScore != null ? newScore : req.score()
            ));
        } catch (Exception e) {
            log.error("Redis error on addScore: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Redis unavailable"));
        }
    }

    @GetMapping("/top")
    public ResponseEntity<?> top(@RequestParam(defaultValue = "10") int limit) {
        try {
            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet()
                            .reverseRangeWithScores(LEADERBOARD_KEY, 0, limit - 1);

            List<Map<String, Object>> result = new ArrayList<>();
            if (tuples != null) {
                int rank = 1;
                for (ZSetOperations.TypedTuple<String> t : tuples) {
                    result.add(Map.of(
                            "rank", rank++,
                            "player", t.getValue() != null ? t.getValue() : "",
                            "score", t.getScore() != null ? t.getScore() : 0.0
                    ));
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Redis error on top: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Redis unavailable"));
        }
    }

    @GetMapping("/rank/{player}")
    public ResponseEntity<?> rank(@PathVariable String player) {
        try {
            Long rank = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, player);
            Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, player);
            if (rank == null || score == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of(
                    "player", player,
                    "rank", rank + 1,
                    "score", score
            ));
        } catch (Exception e) {
            log.error("Redis error on rank: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Redis unavailable"));
        }
    }

    @GetMapping("/player/{player}")
    public ResponseEntity<?> playerScore(@PathVariable String player) {
        try {
            Double score = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, player);
            if (score == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("player", player, "score", score));
        } catch (Exception e) {
            log.error("Redis error on playerScore: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Redis unavailable"));
        }
    }

    // Delete
    @DeleteMapping
    public ResponseEntity<?> clear() {
        try {
            redisTemplate.delete(LEADERBOARD_KEY);
            return ResponseEntity.ok(Map.of("status", "cleared"));
        } catch (Exception e) {
            log.error("Redis error on clear: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Redis unavailable"));
        }
    }
}
