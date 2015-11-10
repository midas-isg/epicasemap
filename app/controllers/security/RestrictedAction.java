package controllers.security;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class RestrictedAction extends Action<Restricted> {
    public F.Promise<Result> call(Context ctx) {
        try {
        	writeAccesses(ctx);
            return delegate.call(ctx);
        } catch(RuntimeException|Error e) {
            throw e;
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }

	private void writeAccesses(Context ctx) {
		AuthorizationKit.writeAccesses(ctx, configuration.value());
	}	
}