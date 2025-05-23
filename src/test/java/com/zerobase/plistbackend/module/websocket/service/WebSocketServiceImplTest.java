package com.zerobase.plistbackend.module.websocket.service;

import static com.zerobase.plistbackend.module.channel.type.ChannelStatus.CHANNEL_STATUS_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.zerobase.plistbackend.module.channel.entity.Channel;
import com.zerobase.plistbackend.module.channel.repository.ChannelRepository;
import com.zerobase.plistbackend.module.participant.entity.Participant;
import com.zerobase.plistbackend.module.user.entity.User;
import com.zerobase.plistbackend.module.user.exception.OAuth2UserException;
import com.zerobase.plistbackend.module.user.repository.UserRepository;
import com.zerobase.plistbackend.module.websocket.dto.request.ChatMessageRequest;
import com.zerobase.plistbackend.module.websocket.dto.response.ChatMessageResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
@Transactional
class WebSocketServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @InjectMocks
  private WebSocketServiceImpl webSocketService;

  @Test
  @DisplayName("유저는 채팅을 보낼 수 있다")
  void success_sendMessage() {
    // given
    String email = "testSender";
    String userImage = "TestImg.img";


    ChatMessageRequest request = ChatMessageRequest.builder()
        .email(email)
        .message("test message")
        .build();

    User mockUser = User.builder()
        .userId(1L)
        .userName(email)
        .userImage(userImage)
        .build();

    // when
    when(userRepository.findByUserEmail(email)).thenReturn(mockUser);
    ChatMessageResponse response = webSocketService.sendMessage(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getSender()).isEqualTo(email);
    assertThat(response.getUserProfileImg()).isEqualTo(userImage);
  }

  @Test
  @DisplayName("유저가 아닌 회원이 메세지를 보내면 Exception이 발생한다")
  void fail_sendMessage() {
    // given
    String email = "NotUser";

    ChatMessageRequest request = ChatMessageRequest.builder()
        .email(email)
        .message("test message")
        .build();

    // when
    when(userRepository.findByUserEmail(email)).thenReturn(null);


    // then: OAuth2UserException 예외 발생 검증
    assertThatThrownBy(() -> webSocketService.sendMessage(request))
        .isInstanceOf(OAuth2UserException.class)
        .hasMessageContaining("해당 유저는 존재하지 않는 유저입니다.");
  }

  @Test
  @DisplayName("비디오 컨트롤을 요청한 유저가 호스트일 경우 true를 반환한다")
  void success_isHost() {
    // given
    Long channelId = 1L;

    User mockUser = User.builder()
        .userEmail("testUser@email.com")
        .userId(1L)
        .build();

    userRepository.save(mockUser);

    Channel mockChannel = Channel.builder()
        .channelId(1L)
        .channelParticipants(List.of(Participant.builder()
            .participantId(1L)
            .isHost(true)
            .user(mockUser)
            .build()))
        .channelHost(mockUser)
        .build();

    channelRepository.save(mockChannel);

    when(channelRepository.findByChannelIdAndChannelStatus(channelId,
        CHANNEL_STATUS_ACTIVE)).thenReturn(Optional.of(mockChannel));

    when(userRepository.findByUserEmail(mockUser.getUserEmail())).thenReturn(mockUser);

    // when
    boolean result = webSocketService.isHost(channelId, mockUser.getUserEmail());

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("비디오 컨트롤을 요청한 유저가 호스트가 아닐 경우 false를 반환한다")
  void fail_isHost() {
    // given
    Long channelId = 1L;

    User hostUser = User.builder()
        .userId(1L)
        .userEmail("hostUser@email.com")
        .build();

    Channel mockChannel = Channel.builder()
        .channelId(1L)
        .channelHost(hostUser)
        .build();
    channelRepository.save(mockChannel);


    User user = User.builder()
        .userId(2L)
        .userEmail("testUser@email.com")
        .build();
    userRepository.save(user);

    Participant participant = Participant.builder()
        .channel(mockChannel)
        .user(user)
        .isHost(false)
        .build();

    mockChannel.getChannelParticipants().add(participant);

    when(channelRepository.findByChannelIdAndChannelStatus(channelId,
        CHANNEL_STATUS_ACTIVE)).thenReturn(Optional.of(mockChannel));

    // when
    boolean result = webSocketService.isHost(channelId, "testUser@email.com");

    // then
    assertThat(result).isFalse();
  }
}
