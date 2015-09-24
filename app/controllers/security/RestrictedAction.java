package controllers.security;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import play.libs.F;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import controllers.security.Restricted.Access;

public class RestrictedAction extends Action<Restricted> {
    public F.Promise<Result> call(Context ctx) {
        try {
        	Access[] accesses = configuration.value();
        	final String join = join(Arrays.asList(accesses));
        	ctx.args.put(Restricted.KEY, join);
        	ctx.args.put("id", ctx.session().get("id"));
            return delegate.call(ctx);
        } catch(RuntimeException|Error e) {
            throw e;
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }

	private String join(List<Access> accesses) {
		return accesses.stream()
				.map(a -> a.toString())
				.collect(Collectors.joining(","));
	}
}