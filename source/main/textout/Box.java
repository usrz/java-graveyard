package textout;
public class Box extends Document {
    private boolean separate = false;

    private String cornerTL = "+";
    private String cornerTR = "+";
    private String cornerBL = "+";
    private String cornerBR = "+";
    private char ruleT = '-';
    private char ruleB = '-';

    public Box(Style parent) {
        super(parent);
        this.setLeftQuote("| ");
        this.setRightQuote(" |");
    }

    public boolean processAttribute(String name, String value) {
        if (name.equals("separate")) {
            if (value.equals("true")) this.separate = true;
            else if (value.equals("false")) this.separate = false;
            else throw new IllegalArgumentException ("Invalid value \"" +
                value + "\" for \"separate\" attribute");
            return(true);
        }

        if (name.equals("corners")) {
            switch (value.length()) {
                case(1):
                    this.cornerTL = value.substring(0,1);
                    this.cornerTR = this.cornerTL;
                    this.cornerBL = this.cornerTL;
                    this.cornerBR = this.cornerTR;
                    break;
                case(4):
                    this.cornerTL = value.substring(0,1);
                    this.cornerTR = value.substring(1,2);
                    this.cornerBR = value.substring(2,3);
                    this.cornerBL = value.substring(3,4);
                    break;
                default:
                    throw new IllegalArgumentException ("Invalid value \"" +
                        value + "\" in corners attribute");
            }
            return(true);
        }

        if (name.equals("rules")) {
            switch (value.length()) {
                case(1):
                    this.ruleT = value.charAt(0);
                    this.setRightQuote(" " + value.substring(0,1));
                    this.ruleB = this.ruleT;
                    this.setLeftQuote(value.substring(0,1)+ " ");
                    break;
                case(2):
                    this.ruleT = value.charAt(0);
                    this.setRightQuote(" " + value.substring(1,2));
                    this.ruleB = this.ruleT;
                    this.setLeftQuote(value.substring(1,2) + " ");
                    break;
                case(4):
                    this.ruleT = value.charAt(0);
                    this.setRightQuote(" " + value.substring(1,2));
                    this.ruleB = value.charAt(2);
                    this.setLeftQuote(value.substring(3,4) + " ");
                    break;
                default:
                    throw new IllegalArgumentException ("Invalid value \"" +
                        value + "\" in corners attribute");
            }
            return(true);
        }

        return(super.processAttribute(name, value));
    }

    public char applyTop(TextWriter out) {
        this.apply(out);
        out.setLeftQuote(this.cornerTL);
        out.setRightQuote(this.cornerTR);
        return(this.ruleT);
    }

    public char applyBottom(TextWriter out) {
        this.apply(out);
        out.setLeftQuote(this.cornerBL);
        out.setRightQuote(this.cornerBR);
        return(ruleB);
    }

    public boolean isSeparated() {
        return(this.separate);
    }
}
