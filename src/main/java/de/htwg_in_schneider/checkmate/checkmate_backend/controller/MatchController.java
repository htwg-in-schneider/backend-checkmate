package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;



import de.htwg_in_schneider.checkmate.checkmate_backend.model.MatchDecision;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.User;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.MatchDecisionRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final UserRepository userRepo;
    private final MatchDecisionRepository decisionRepo;

    public MatchController(UserRepository userRepo, MatchDecisionRepository decisionRepo) {
        this.userRepo = userRepo;
        this.decisionRepo = decisionRepo;
    }
    @GetMapping("/like")
public List<User> mySentLikes(@AuthenticationPrincipal Jwt jwt) {
    User me = userRepo.findByOauthId(jwt.getSubject()).orElseThrow();

    // Hole alle Entscheidungen, bei denen ich der Absender bin und "LIKE" gewählt habe
    List<MatchDecision> myLikes = decisionRepo.findByFromUser_IdAndDecision(
            me.getId(), MatchDecision.Decision.LIKE
    );

    // Wandle die Liste von MatchDecision in eine Liste von User (die Empfänger) um
    return myLikes.stream()
            .map(MatchDecision::getToUser)
            .toList();
}
    @PostMapping("/like")
    public DecisionResponse like(@AuthenticationPrincipal Jwt jwt,
                                 @RequestBody DecisionRequest req) {

        User me = userRepo.findByOauthId(jwt.getSubject()).orElseThrow();
        User target = userRepo.findById(req.targetUserId()).orElseThrow();

        upsertDecision(me, target, MatchDecision.Decision.LIKE);

        // MATCH wenn Gegenrichtung auch LIKE ist
        boolean matched = decisionRepo.existsByFromUser_IdAndToUser_IdAndDecision(
                target.getId(), me.getId(), MatchDecision.Decision.LIKE
        );

        return new DecisionResponse(matched);
    }

    @PostMapping("/dislike")
    public void dislike(@AuthenticationPrincipal Jwt jwt,
                        @RequestBody DecisionRequest req) {

        User me = userRepo.findByOauthId(jwt.getSubject()).orElseThrow();
        User target = userRepo.findById(req.targetUserId()).orElseThrow();

        upsertDecision(me, target, MatchDecision.Decision.DISLIKE);
    }

    private void upsertDecision(User me, User target, MatchDecision.Decision d) {
        MatchDecision dec = decisionRepo
                .findByFromUser_IdAndToUser_Id(me.getId(), target.getId())
                .orElseGet(MatchDecision::new);

        dec.setFromUser(me);
        dec.setToUser(target);
        dec.setDecision(d);

        decisionRepo.save(dec);
    }

    // "Meine Matches": alle, die ich geliked habe UND die mich geliked haben
    @GetMapping("/me")
    public List<User> myMatches(@AuthenticationPrincipal Jwt jwt) {
        User me = userRepo.findByOauthId(jwt.getSubject()).orElseThrow();

        // 1) Ich like -> targets
        List<MatchDecision> myLikes = decisionRepo.findByFromUser_IdAndDecision(
                me.getId(), MatchDecision.Decision.LIKE
        );

        // 2) Filtere nur die targets, die mich auch geliked haben
        return myLikes.stream()
                .map(MatchDecision::getToUser)
                .filter(u ->
                        decisionRepo.existsByFromUser_IdAndToUser_IdAndDecision(
                                u.getId(), me.getId(), MatchDecision.Decision.LIKE
                        )
                )
                .toList();
    }
    @DeleteMapping("/like/{userId}")
    public ResponseEntity<Void> removeLikeOrMatch(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long userId) { 
         String myOauthId = jwt.getSubject();

         User me = userRepo.findByOauthId(myOauthId)
            .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

         Long meId = me.getId();
         Long otherId = userId;

    // 1) meinen Like löschen (Warten entfernen)
         decisionRepo.findByFromUser_IdAndToUser_Id(meId, otherId)
            .ifPresent(decisionRepo::delete);

    // 2) falls es ein Match war: den Gegen-Like auch löschen (Match auflösen)
          decisionRepo.findByFromUser_IdAndToUser_Id(otherId, meId)
            .ifPresent(decisionRepo::delete);

         return ResponseEntity.noContent().build();
    }
}

record DecisionRequest(Long targetUserId) {}
record DecisionResponse(boolean matched) {}
