package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.ChatResponse;
import edu.utec.planificador.dto.response.ReportResponse;
import edu.utec.planificador.dto.response.SuggestionsResponse;

public interface AIAgentService {

    ChatResponse sendChatMessage(String sessionId, String message, Long courseId);

    SuggestionsResponse getSuggestions(Long courseId);

    ReportResponse generateReport(Long courseId);

    void clearChatSession(String sessionId);
}

