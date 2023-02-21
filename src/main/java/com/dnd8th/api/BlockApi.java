package com.dnd8th.api;


import com.dnd8th.dto.MainDTO;
import com.dnd8th.dto.MainWeekDTO;
import com.dnd8th.dto.block.BlockCreateRequest;
import com.dnd8th.service.BlockService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlockApi {

    private final BlockService blockService;

    @PostMapping("")
    public ResponseEntity<String> createBlock(
            @RequestBody @Valid BlockCreateRequest blockCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        blockService.createBlock(blockCreateRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("");
    }

    @GetMapping("/{date}")
    public ResponseEntity
            <MainWeekDTO> getMainWeek(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("date") String date) {
        String email = userDetails.getUsername();
        MainWeekDTO mainWeek = blockService.getBlockWeek(email, date);
        return ResponseEntity.status(HttpStatus.OK).body(mainWeek);
    }

    @GetMapping("/detail/{date}")
    public ResponseEntity
            <MainDTO> getMainDetail(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("date") String date) {
        String email = userDetails.getUsername();
        MainDTO mainDto = blockService.getBlockDetail(email, date);
        return ResponseEntity.status(HttpStatus.OK).body(mainDto);
    }
}
