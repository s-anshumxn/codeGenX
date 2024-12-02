package com.example.pythonapiplugin;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Migration extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            try {
                window.getActivePage().showView("com.example.pythonapiplugin.MigrationView");
            } catch (PartInitException e) {
                throw new ExecutionException("Error opening Migration view", e);
            }
        }
        return null;
    }
}
