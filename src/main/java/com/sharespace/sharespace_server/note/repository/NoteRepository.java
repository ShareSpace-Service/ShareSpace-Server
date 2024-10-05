package com.sharespace.sharespace_server.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sharespace.sharespace_server.note.entity.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
	List<Note> findAllByReceiverId(Long userId);
}
