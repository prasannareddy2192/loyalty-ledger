package com.prasanna.ledger.member;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository members;

    public MemberController(MemberRepository members) {
        this.members = members;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member create(@Valid @RequestBody CreateMemberRequest request) {
        return members.save(new Member(request.name(), request.email()));
    }
}