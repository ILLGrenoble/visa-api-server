package eu.ill.visa.web.rest.dtos;

import jakarta.validation.constraints.Size;

public class JupyterNotebookSessionInput {

    @Size(max = 150)
    private String kernelId;

    @Size(max = 150)
    private String sessionId;

    public JupyterNotebookSessionInput() {
    }

    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
