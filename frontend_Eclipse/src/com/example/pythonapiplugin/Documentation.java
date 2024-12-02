package com.example.pythonapiplugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.gson.JsonObject;

public class Documentation extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            IEditorPart editor = page.getActiveEditor();
            if (editor instanceof ITextEditor) {
                ITextEditor textEditor = (ITextEditor) editor;
                IDocumentProvider provider = textEditor.getDocumentProvider();
                IDocument document = provider.getDocument(editor.getEditorInput());
                ISelection selection = textEditor.getSelectionProvider().getSelection();

                if (selection instanceof ITextSelection) {
                    ITextSelection textSelection = (ITextSelection) selection;
                    String selectedText = textSelection.getText();

                    Display display = Display.getDefault();
                    Shell popupShell = new Shell(display, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
                    popupShell.setText("Documenting");
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
                        try {
                            JsonObject jsonRequest = new JsonObject();
                            jsonRequest.addProperty("code", selectedText); // Assuming 'code' is the key expected by your API

                            String apiResponse = sendToApi(jsonRequest.toString(), "");
                            insertApiResponse(document, textSelection.getOffset(), apiResponse);

                            display.asyncExec(() -> {
                                // Update status label to "Completed"
                                statusLabel.setText("Completed");

                                // Center align "Completed" label and add timer to close popup
                                GridData completedGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
                                statusLabel.setLayoutData(completedGridData);
                                display.timerExec(1000, () -> popupShell.close());
                            });
                        } catch (Exception ex) {
                            display.asyncExec(() -> {
                                // Update status label to show error message
                                statusLabel.setText("Error: " + ex.getMessage());

                                // Center align error message and add close button
                                GridData errorGridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
                                statusLabel.setLayoutData(errorGridData);

                                // Add close button
                                Button closeButton = new Button(popupShell, SWT.PUSH);
                                closeButton.setText("Close");
                                closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
                                closeButton.addListener(SWT.Selection, e -> popupShell.close());

                                // Pack shell to adjust size based on added components
                                popupShell.pack();
                            });
                        }
                    });

                    popupShell.open();
                }
            }
        }
        return null;
    }

    private String sendToApi(String jsonPayload, String params) throws IOException, URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8000/refactor/");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new IOException("Failed to fetch data from API. Response code: " + responseCode);
        }
    }

    private void insertApiResponse(IDocument document, int offset, String apiResponse) {
        Display.getDefault().asyncExec(() -> {
            try {
                final String formattedResponse = apiResponse.replace("\\n", System.lineSeparator());

                document.replace(offset, 0, formattedResponse + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exceptions
            }
        });
    }
}
