package com.klb.app.web.chat;

import com.klb.app.application.service.chat.InternalChatService;
import com.klb.app.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

	private final InternalChatService internalChatService;
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/chat.send")
	public void send(@Payload ChatSendStompRequest request, Principal principal) {
		if (!(principal instanceof ChatPrincipal p)) {
			return;
		}
		try {
			var dto = internalChatService.appendMessage(request.roomCode(), p.getUserId(), p.getName(), request.body());
			messagingTemplate.convertAndSend("/topic/chat/" + dto.roomCode(), dto);
		} catch (DomainException e) {
			messagingTemplate.convertAndSendToUser(p.getName(), "/queue/chat/errors", new ChatErrorPayload(e.getMessage()));
		}
	}
}
