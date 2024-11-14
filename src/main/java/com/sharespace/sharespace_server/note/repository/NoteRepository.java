package com.sharespace.sharespace_server.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.note.entity.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
	@Query("SELECT n FROM Note n WHERE n.receiver.id = :userId ORDER BY n.send_at DESC")
	List<Note> findAllByReceiverId(Long userId);

	@Query("SELECT COUNT(*) FROM Note n Where n.isRead = false AND n.receiver.id = :userId")
	int findCountUnreadNotesByReceiverId(Long userId);
}
