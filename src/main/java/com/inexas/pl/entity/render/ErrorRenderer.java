package com.inexas.pl.entity.render;

public class ErrorRenderer extends AbstractRenderer {
	private final Throwable exception;

	public ErrorRenderer(Throwable exception) {
		this.exception = exception;
	}

	public void toHtml(int startingOffset) {
		sb.setLength(startingOffset);
		
		final String message = exception.getMessage();
		if(message != null) {
			write(exception.getMessage());
		}
		write(exception.getClass().getName());
		sb.append("<blockquote>");
		final StackTraceElement elements[] = exception.getStackTrace();
		for(int i = 0; i < elements.length; i++) {
			final StackTraceElement element = elements[i];
			write(element.toString());
		}
		sb.append("</blockquote>");
	}

	private void write(String message) {
		sb.append(message);
		sb.append("<br/>");
	}

	@Override
    public void render() {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

	@Override
    public void renderField(String id) {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

	@Override
    public void renderLabel() {
	    // !todo Implement me
	    throw new RuntimeException("How about implementing me?!");
    }

}
