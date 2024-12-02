package com.example.pythonapiplugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class RefactorView extends ViewPart {
    public static final String ID = "com.example.pythonapiplugin.RefactorView";

    private Text textArea;
    private Text responseArea;
    private Button btnSendToApi;
    private Button btnClear;

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(2, false)); // Change to 2 columns
        Color lightGray = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
        Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);

        // Text Editor Content label and text area
        Label lblTextEditorContent = new Label(parent, SWT.NONE);
        lblTextEditorContent.setText("Text Editor Content:");
        lblTextEditorContent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1)); // Span 2 columns

        textArea = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        textArea.setBackground(white);
        GridData textAreaLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1); // Span 2 columns
        textAreaLayoutData.heightHint = 200; // Adjust height as needed
        textArea.setLayoutData(textAreaLayoutData);
        textArea.setToolTipText("You can also select the part of code you want to refactor and paste it here.");

        // Populate text area from active editor
        populateTextAreaFromActiveEditor();

        // Enable Send to API button if text area is not empty
        textArea.addModifyListener(e -> btnSendToApi.setEnabled(!textArea.getText().isEmpty()));

        // Send to API button and Clear button
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, true)); // 2 columns for buttons
        buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1)); // Span 2 columns

        btnSendToApi = new Button(buttonComposite, SWT.PUSH);
        btnSendToApi.setText("Refactor");
        btnSendToApi.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnSendToApi.setEnabled(!textArea.getText().isEmpty()); // Enable based on initial text
        btnSendToApi.addListener(SWT.Selection, event -> sendToApi());

        btnClear = new Button(buttonComposite, SWT.PUSH);
        btnClear.setText("Clear");
        btnClear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnClear.addListener(SWT.Selection, event -> clearTextAreas());

        // API Response label and text area
        Label lblApiResponse = new Label(parent, SWT.NONE);
        lblApiResponse.setText("Refactored Code:");
        lblApiResponse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1)); // Span 2 columns

        responseArea = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        responseArea.setBackground(lightGray);
        GridData responseAreaLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1); // Span 2 columns
        responseAreaLayoutData.heightHint = textAreaLayoutData.heightHint; // Match height with textArea
        responseArea.setLayoutData(responseAreaLayoutData);
        responseArea.setToolTipText("The refactored code will be displayed here.");
    }

    private void populateTextAreaFromActiveEditor() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                IEditorPart editorPart = page.getActiveEditor();
                if (editorPart instanceof ITextEditor) {
                    ITextEditor textEditor = (ITextEditor) editorPart;
                    IDocumentProvider provider = textEditor.getDocumentProvider();
                    try {
                        String javaCode = "";
                        if (editorPart instanceof ITextEditor && ((ITextEditor) editorPart).getSelectionProvider().getSelection() instanceof ITextSelection) {
                            ITextSelection selection = (ITextSelection) ((ITextEditor) editorPart).getSelectionProvider().getSelection();
                            javaCode = selection.getText().isEmpty() ? provider.getDocument(textEditor.getEditorInput()).get() : selection.getText();
                        } else {
                            javaCode = provider.getDocument(textEditor.getEditorInput()).get();
                        }
                        textArea.setText(javaCode);
                    } catch (Exception e) {
                        textArea.setText("Active editor is not a text editor.");
                    }
                }
            }
        }
    }

    private void sendToApi() {
        Display display = Display.getDefault();
        Shell popupShell = new Shell(display, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
        popupShell.setText("Refactoring");
        popupShell.setLayout(new GridLayout(1, false));

        Label statusLabel = new Label(popupShell, SWT.NONE);
        statusLabel.setText("Running...");
        statusLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        popupShell.setSize(400, 120);

        Rectangle bounds = display.getPrimaryMonitor().getBounds();
        Rectangle rect = popupShell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        popupShell.setLocation(x, y);

        display.asyncExec(() -> {
            String content = textArea.getText();
            try {
                String response = sendContentToApi(content);
                responseArea.setText(response.replace("\\n", System.lineSeparator()).replace("\\\"", "\""));
                display.asyncExec(() -> {
                    // Update status label to "Completed"
                    statusLabel.setText("Completed");

                    // Center align "Completed" label and add timer to close popup
                    statusLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
                    display.timerExec(1000, () -> popupShell.close());
                });
            } catch (IOException | URISyntaxException e) {
                display.asyncExec(() -> {
                    // Update status label to show error message
                    statusLabel.setText("Error: " + e.getMessage());

                    // Center align error message and add close button
                    statusLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

                    // Add close button
                    Button closeButton = new Button(popupShell, SWT.PUSH);
                    closeButton.setText("Close");
                    closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
                    closeButton.addListener(SWT.Selection, event -> popupShell.close());

                    // Pack shell to adjust size based on added components
                    popupShell.pack();
                });
            }
        });

        popupShell.open();
    }

    private String sendContentToApi(String content) throws IOException, URISyntaxException {
        URI uri = new URI(Refactor.API_ENDPOINT_URL);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        // Create JSON payload
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("code", content);

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
                return response.toString();
            }
        } else {
            throw new IOException("Failed to get response from API. HTTP response code: " + responseCode);
        }
    }

    private void clearTextAreas() {
        textArea.setText("");
        responseArea.setText("");
    }

    @Override
    public void setFocus() {
        textArea.setFocus();
    }
}
