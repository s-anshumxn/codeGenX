package com.example.pythonapiplugin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class Refactor extends AbstractHandler {
	static final String API_ENDPOINT_URL = "http://127.0.0.1:8000/try/documentation/?usecase=documentation";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            // Open the custom view
            IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
            window.getActivePage().showView(RefactorView.ID);

        } catch (PartInitException e) {
            e.printStackTrace();
            throw new ExecutionException("Failed to open the custom view", e);
        }
        return null;
    }
}