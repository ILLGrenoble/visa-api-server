package eu.ill.visa.web.dtos;

import javax.validation.constraints.Size;

public class JupyterNotebookSessionDto {

    @Size(max = 150)
    private String kernelId;

    @Size(max = 150)
    private String sessionId;

    public JupyterNotebookSessionDto() {
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
