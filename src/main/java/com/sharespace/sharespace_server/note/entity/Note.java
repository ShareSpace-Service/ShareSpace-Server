package com.sharespace.sharespace_server.note.entity;

import java.time.LocalDateTime;

import com.sharespace.sharespace_server.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "note", schema = "sharespace")
@Getter
@Setter
@NoArgsConstructor
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender", nullable = false)
	public User sender;		// 송신자

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver", nullable = false)
	public User receiver;		// 수신자

	@Column(name = "title", nullable = false)
	public String title;

	@Column(name = "content", nullable = false)
	public String content;

	@Column(name = "send_at", nullable = false)
	public LocalDateTime send_at;

	@Column(name = "is_read", nullable = false)
	public boolean isRead;

	@Builder
	public Note(User sender, User receiver, String title, String content, LocalDateTime send_at) {
		this.sender = sender;
		this.receiver = receiver;
		this.title = title;
		this.content = content;
		this.send_at = send_at;
	}

	public static Note create(User sender, User receiver, String title, String content) {
		return Note.builder()
			.sender(sender)
			.receiver(receiver)
			.title(title)
			.content(content)
			.send_at(LocalDateTime.now())
			.build();
	}
}
