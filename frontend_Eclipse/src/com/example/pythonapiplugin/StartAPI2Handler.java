package com.example.pythonapiplugin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class StartAPI2Handler extends AbstractHandler {

    private Process apiProcess;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        startAPIProcess("C:\\Users\\anshuman singh\\Downloads\\fid\\api2.py");
        
        try {
            String apiResponse = sendRequestToAPI("http://localhost:5001/api2");
            showMessage("API Response", apiResponse);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new ExecutionException("Failed to communicate with Python API server", e);
        }
        
        return null;
    }

    private void startAPIProcess(String pythonScriptPath) throws ExecutionException {
        try {
            if (apiProcess == null || !apiProcess.isAlive()) {
                ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);
                apiProcess = processBuilder.start();
                System.out.println("API server started.");
            } else {
                System.out.println("API server is already running.");
                Display.getDefault().asyncExec(() -> {
                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), "API Server", "API server is already running.");
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExecutionException("Failed to start Python API server", e);
        }
    }

    private String sendRequestToAPI(String endpointUrl) throws IOException, URISyntaxException {
        URI uri = new URI(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("Failed to fetch data from API. Response code: " + responseCode);
        }
    }

    private void showMessage(String title, String message) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        display.asyncExec(() -> {
            Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
            shell.setText(title);
            shell.setLayout(new FillLayout());

            // Create a scrolled composite
            ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);

            // Create a text widget inside the scrolled composite
            Text text = new Text(scrolledComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
            text.setText(message);

            // Set the content of the scrolled composite
            scrolledComposite.setContent(text);
            scrolledComposite.setMinSize(text.computeSize(SWT.DEFAULT, SWT.DEFAULT));

            // Open the shell
            shell.setSize(400, 300);
            shell.open();
        });
    }
}
