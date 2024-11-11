package aimo.backend.domains.privatePost.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aimo.backend.domains.privatePost.dto.parameter.TextRecordParameter;
import aimo.backend.domains.privatePost.entity.TextRecord;
import aimo.backend.domains.privatePost.repository.TextRecordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextRecordService {

	private final TextRecordRepository textRecordRepository;

	@Transactional(rollbackFor = Exception.class)
	public void save(TextRecordParameter parameter) {
		TextRecord textRecord = TextRecord.from(parameter);
		textRecordRepository.save(textRecord);
	}
}
