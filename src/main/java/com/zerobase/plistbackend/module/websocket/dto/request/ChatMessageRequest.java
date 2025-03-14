package com.zerobase.plistbackend.module.websocket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
  private String email;
  private String message;
  private Long channelId;

  public void allocateChannelId(Long channelId) {
    this.channelId = channelId;
  }
}
