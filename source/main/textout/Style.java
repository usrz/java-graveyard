package textout;

import org.xml.sax.Attributes;

public abstract class Style {
    private Style parent = null;

    private int indent = TextWriter.INDENT_DEFAULT;
    private int margin = TextWriter.MARGIN_DEFAULT;
    private int alignment = TextWriter.ALIGN_DEFAULT;

    private String bullet = TextWriter.BULLET_DEFAULT;
    private String leftQuote = TextWriter.LEFT_QUOTE_DEFAULT;
    private String innerQuote = TextWriter.INNER_QUOTE_DEFAULT;
    private String rightQuote = TextWriter.RIGHT_QUOTE_DEFAULT;

    public Style(Style parent) {
        super();

        if (parent != null) {
            indent = parent.indent;
            margin = parent.margin;
            bullet = parent.bullet;
            leftQuote = parent.leftQuote;
            innerQuote = parent.innerQuote;
            rightQuote = parent.rightQuote;
            alignment = parent.alignment;
        }
        this.parent = parent;
    }

    public Style processAttributes(Attributes attributes) {
        if (attributes == null) return(this);

        for (int x = 0; x < attributes.getLength(); x++) {
            String name = attributes.getQName(x);
            String value = attributes.getValue(x);
            if (! processAttribute(name, value)) {
                throw new IllegalArgumentException("Invalid attribute " +
                    name + "=\"" + value + "\" specified");
            }
        }
        return (this);
    }

    public boolean processAttribute(String name, String value) {
        return(false);
    }

    public Style getParent() {
        return(parent);
    }

    public void setIndent(String indent) {
        if (indent != null) {
            if (indent.startsWith("+")) {
                this.indent += Integer.parseInt(indent.substring(1));
            } else if (indent.startsWith("-")) {
                this.indent -= Integer.parseInt(indent.substring(1));
            } else {
                this.indent = Integer.parseInt(indent);
            }
        }
    }

    public void setMargin(String margin) {
        if (margin != null) {
            if (margin.startsWith("+")) {
                this.margin += Integer.parseInt(margin.substring(1));
            } else if (margin.startsWith("-")) {
                this.margin -= Integer.parseInt(margin.substring(1));
            } else {
                this.margin = Integer.parseInt(margin);
            }
        }
    }

    public void setAlignment(String align) {
        if (align != null) {
            if (align.equals("left")) {
                alignment = TextWriter.ALIGN_LEFT;
            } else if (align.equals("right")) {
                alignment = TextWriter.ALIGN_RIGHT;
            } else if (align.equals("center")) {
                alignment = TextWriter.ALIGN_CENTER;
            } else if (align.equals("justify")) {
                alignment = TextWriter.ALIGN_JUSTIFY;
            } else {
                throw new IllegalArgumentException ("Invalid value \"" +
                        align + "\" in align property");
            }
        }
    }

    public void setBullet(String bullet) {
        if (bullet != null) this.bullet = bullet;
    }

    public String getBullet() {
        return(bullet);
    }

    public void setLeftQuote(String quote) {
        if (quote != null) leftQuote = quote;
    }

    public String getLeftQuote() {
        return(leftQuote);
    }

    public void setInnerQuote(String quote) {
        if (quote != null) innerQuote = quote;
    }

    public String getInnerQuote() {
        return(innerQuote);
    }

    public void setRightQuote(String quote) {
        if (quote != null) rightQuote = quote;
    }

    public String getRightQuote() {
        return(rightQuote);
    }

    public void apply(TextWriter writer) {
        writer.setIndent(indent);
        writer.setMargin(margin);
        writer.setBullet(bullet);
        writer.setLeftQuote(leftQuote);
        writer.setInnerQuote(innerQuote);
        writer.setRightQuote(rightQuote);
        writer.setAlignment(alignment);
    }
}
