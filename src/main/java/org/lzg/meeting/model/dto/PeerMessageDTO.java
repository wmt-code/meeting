package org.lzg.meeting.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PeerMessageDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private String signalType; // "offer"、"answer"、"candidate"
	private String signalData;
}
