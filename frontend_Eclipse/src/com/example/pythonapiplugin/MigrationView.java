package com.example.pythonapiplugin;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class MigrationView extends ViewPart {
	public static final String ID = "com.example.pythonapiplugin.MigrationView";

    private Text textArea;
    private Text promptArea;
    private Text responseArea;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FormLayout());
        Color lightGray = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
        Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);

        // Text Editor Content label and text area
        Label lblTextEditorContent = new Label(parent, SWT.NONE);
        lblTextEditorContent.setText("Text Editor Content:");
        FormData fd_lblTextEditorContent = new FormData();
        fd_lblTextEditorContent.top = new FormAttachment(0, 10);
        fd_lblTextEditorContent.left = new FormAttachment(0, 10);
        lblTextEditorContent.setLayoutData(fd_lblTextEditorContent);

        textArea = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        textArea.setBackground(white);
        FormData fd_textArea = new FormData();
        fd_textArea.top = new FormAttachment(lblTextEditorContent, 6);
        fd_textArea.left = new FormAttachment(0, 10);
        fd_textArea.right = new FormAttachment(100, -10);
        fd_textArea.bottom = new FormAttachment(33, -10); // Covers upper half
        textArea.setLayoutData(fd_textArea);

        // Populate text area with current text editor content
        IEditorPart editorPart = getSite().getPage().getActiveEditor();
        if (editorPart instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editorPart;
            IDocumentProvider provider = textEditor.getDocumentProvider();
            IDocument document = provider.getDocument(textEditor.getEditorInput());

            // Get selected text if available, otherwise get full content
            String selectedText = ((ITextSelection) textEditor.getSelectionProvider().getSelection()).getText();
            if (selectedText != null && !selectedText.isEmpty()) {
                textArea.setText(selectedText);
            } else {
                textArea.setText(document.get());
            }
        }

        // Prompt label and text area
        Label lblPrompt = new Label(parent, SWT.NONE);
        lblPrompt.setText("Prompt:");
        FormData fd_lblPrompt = new FormData();
        fd_lblPrompt.top = new FormAttachment(textArea, 10);
        fd_lblPrompt.left = new FormAttachment(0, 10);
        lblPrompt.setLayoutData(fd_lblPrompt);

        promptArea = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        promptArea.setBackground(white);
        FormData fd_promptArea = new FormData();
        fd_promptArea.top = new FormAttachment(lblPrompt, 6);
        fd_promptArea.left = new FormAttachment(0, 10);
        fd_promptArea.right = new FormAttachment(100, -10);
        fd_promptArea.bottom = new FormAttachment(66, -10); // Adjust the bottom attachment to cover upper-middle half
        promptArea.setLayoutData(fd_promptArea);

        // Tooltips
        textArea.setToolTipText("You can also select the part of code you want to transform and paste it here.");
        promptArea.setToolTipText("Enter the prompt for the transformation.");
        
        

        // Send to API button
        Button btnSendToApi = new Button(parent, SWT.PUSH);
        btnSendToApi.setText("Transform");
        FormData fd_btnSendToApi = new FormData();
        fd_btnSendToApi.top = new FormAttachment(promptArea, 10);
        fd_btnSendToApi.left = new FormAttachment(50, -50);
        btnSendToApi.setLayoutData(fd_btnSendToApi);
        btnSendToApi.addListener(SWT.Selection, event -> sendToApi());

        // Clear button
        Button btnClear = new Button(parent, SWT.PUSH);
        btnClear.setText("Clear");
        FormData fd_btnClear = new FormData();
        fd_btnClear.top = new FormAttachment(promptArea, 10);
        fd_btnClear.left = new FormAttachment(btnSendToApi, 10);
        btnClear.setLayoutData(fd_btnClear);
        btnClear.addListener(SWT.Selection, event -> {
            textArea.setText("");
            promptArea.setText("");
            responseArea.setText("");
        });
        
        Button btnReplace = new Button(parent, SWT.PUSH);
        btnReplace.setText("Replace");
        FormData fd_btnReplace = new FormData();
        fd_btnReplace.top = new FormAttachment(promptArea, 10);
        fd_btnReplace.left = new FormAttachment(btnClear, 10);
        btnReplace.setLayoutData(fd_btnReplace);
        btnReplace.addListener(SWT.Selection, event -> replaceTextInEditor());

        // API Response label and text area
        Label lblApiResponse = new Label(parent, SWT.NONE);
        lblApiResponse.setText("Transformed Code:");
        FormData fd_lblApiResponse = new FormData();
        fd_lblApiResponse.top = new FormAttachment(btnSendToApi, 10);
        fd_lblApiResponse.left = new FormAttachment(0, 10);
        lblApiResponse.setLayoutData(fd_lblApiResponse);

        responseArea = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        responseArea.setBackground(lightGray);
        FormData fd_responseArea = new FormData();
        fd_responseArea.top = new FormAttachment(lblApiResponse, 6);
        fd_responseArea.left = new FormAttachment(0, 10);
        fd_responseArea.right = new FormAttachment(100, -10);
        fd_responseArea.bottom = new FormAttachment(100, -10); // Covers lower half
        responseArea.setLayoutData(fd_responseArea);

        // Tooltip for response area
        responseArea.setToolTipText("Transformed code will be displayed here.");

        // Set focus to text area
        textArea.setFocus();
    }

    private void sendToApi() {
        Display display = Display.getDefault();
        Shell popupShell = new Shell(display, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
        popupShell.setText("Transforming");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginWidth = 10;
        gridLayout.marginHeight = 10;
        popupShell.setLayout(gridLayout);

        Label statusLabel = new Label(popupShell, SWT.NONE);
        statusLabel.setText("Running...");
        GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        gridData.widthHint = 200;
        statusLabel.setLayoutData(gridData);

        popupShell.setSize(400, 120);

        Rectangle bounds = display.getPrimaryMonitor().getBounds();
        Rectangle rect = popupShell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        popupShell.setLocation(x, y);

        display.asyncExec(() -> {
            String code = textArea.getText();
            String prompt = promptArea.getText();

            try {
                JsonObject response = processApis(code, prompt);
                display.asyncExec(() -> {
                    responseArea.setText(response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
                    statusLabel.setText("Completed");
                    display.timerExec(1000, () -> popupShell.close());
                });
            } catch (IOException | URISyntaxException e) {
                display.asyncExec(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    Button closeButton = new Button(popupShell, SWT.PUSH);
                    closeButton.setText("Close");
                    closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
                    closeButton.addListener(SWT.Selection, event -> popupShell.close());
                    popupShell.pack();
                });
            }
        });

        popupShell.open();
    }
    
    private void replaceTextInEditor() {
        IEditorPart editorPart = getSite().getPage().getActiveEditor();
        if (editorPart instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editorPart;
            IDocumentProvider provider = textEditor.getDocumentProvider();
            IDocument document = provider.getDocument(textEditor.getEditorInput());

            // Replace the entire document content with the text from response area
            document.set(responseArea.getText());
        }
    }

    private JsonObject processApis(String code, String prompt) throws IOException, URISyntaxException {
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("content", "");
        JsonObject api1Response = callApi1(code, prompt, jsonPayload);
        JsonObject api3Response = callApi3(code, prompt, jsonPayload); 
        JsonObject api2Response = callApi2(code, prompt, api1Response);
        JsonObject api4Response = callApi4(code, prompt, api3Response);

        int maxAttempts = 3;
        int api1Attempts = 0;
        int api3Attempts = 0;

        while ((!isFinalAnswerTrue(api2Response) || !isFinalAnswerTrue(api4Response)) &&
                api1Attempts < maxAttempts && api3Attempts < maxAttempts)
        {
            if (!isFinalAnswerTrue(api2Response) && api1Attempts < maxAttempts) {
                api1Response = callApi1(code, prompt, api2Response);
                api2Response = callApi2(code, prompt, api1Response);
                api1Attempts++;
            }

            if (!isFinalAnswerTrue(api4Response) && api3Attempts < maxAttempts) {
            	api3Response = callApi3(code, prompt, api4Response); // Else, call Decompose API again
                api4Response = callApi4(code, prompt, api3Response); // Decompose Validation API
                api3Attempts++;
            }
        }

        // Once final answers are true, proceed to Transform API
        JsonObject api5Response = callApi5(code, prompt, api1Response, api3Response, jsonPayload); // Transform API
        JsonObject api6Response = callApi6(code, prompt, api1Response, api3Response, api5Response);
        int api5Attempts = 0;
        
        while (!isFinalAnswerTrue(api6Response) && api5Attempts < maxAttempts) {
        	api5Response = callApi5(code, prompt, api1Response, api3Response, api6Response); // Else, call Transform API again
        	api6Response = callApi6(code, prompt, api1Response, api3Response, api5Response);
            api5Attempts++;
        }

        return api5Response;
    }


    private boolean isFinalAnswerTrue(JsonObject response) {
        return response.has("final_answer") && response.get("final_answer").getAsBoolean();
    }

    private JsonObject callApi1(String code, String prompt, JsonObject api2Response) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/migration/analysis?usecase=soap-to-rest");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("code", code);
        jsonPayload.addProperty("prompt", prompt);
        jsonPayload.addProperty("analysis_validation", api2Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse; // Replace with actual parsing of response
            }
        } else {
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    private JsonObject callApi2(String code, String prompt,JsonObject api1Response) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/migration/analysis-validation?usecase=soap-to-rest");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("code", code);
        jsonPayload.addProperty("prompt", prompt);
        jsonPayload.addProperty("analysis", api1Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse; // Replace with actual parsing of response
            }
        } else {
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    private JsonObject callApi3(String code, String prompt, JsonObject api4Response) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/migration/decomposition?usecase=soap-to-rest");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("code", code);
        jsonPayload.addProperty("prompt", prompt);
        jsonPayload.addProperty("decomposition_validation", api4Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse; // Replace with actual parsing of response
            }
        } else {
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    private JsonObject callApi4(String code, String prompt,JsonObject api3Response) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/migration/decomposition-validation?usecase=soap-to-rest");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("code", code);
        jsonPayload.addProperty("prompt", prompt);
        jsonPayload.addProperty("decomposition", api3Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse; // Replace with actual parsing of response
            }
        } else {
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    private JsonObject callApi5( String code, String prompt, JsonObject api1Response, JsonObject api3Response, JsonObject api6Response) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/migration/transform?usecase=soap-to-rest");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("prompt", prompt);
        jsonPayload.addProperty("code", code);
        jsonPayload.addProperty("analysis", api1Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
        jsonPayload.addProperty("decomposition", api3Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
        jsonPayload.addProperty("transform_validation", api6Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse; // Replace with actual parsing of response
            }
        } else {
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    private JsonObject callApi6(String code, String prompt,JsonObject api1Response, JsonObject api3Response,JsonObject api5Response) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/migration/transform-validation?usecase=soap-to-rest");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("code", code);
        jsonPayload.addProperty("prompt", prompt);
        jsonPayload.addProperty("analysis", api1Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
        jsonPayload.addProperty("decomposition", api3Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
        jsonPayload.addProperty("transform", api5Response.toString().replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.toString().getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                return jsonResponse; // Replace with actual parsing of response
            }
        } else {
            throw new IOException("API call failed with response code: " + responseCode);
        }
    }

    @Override
    public void setFocus() {
        textArea.setFocus();
    }
}