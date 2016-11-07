package org.verapdf.pd.font.cff;

import org.verapdf.as.io.ASInputStream;
import org.verapdf.as.io.ASMemoryInStream;
import org.verapdf.pd.font.CFFNumber;
import org.verapdf.pd.font.type1.BaseCharStringParser;

import java.io.IOException;

/**
 * This class does basic parsing of Type 2 CharString to extract width value
 * from it.
 *
 * @author Sergey Shemyakov
 */
class Type2CharStringParser extends BaseCharStringParser {

    private static final int TWO_POWER_16 = 65536;

    /**
     * {@inheritDoc}
     */
    Type2CharStringParser(ASInputStream stream) throws IOException {
        super(stream);
    }

    /**
     * {@inheritDoc}
     */
    Type2CharStringParser(ASInputStream stream, CFFIndex localSubrs, int bias,
                          CFFIndex globalSubrs) throws IOException {
        super(stream, localSubrs, bias, globalSubrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean processNextOperator(int nextByte) throws IOException {
        switch (nextByte) {
            case 14:    // endchar
            case 19:    // cntrmask
            case 20:    // hintmask
            case 11:    // return
                if (!this.stack.empty()) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                break;
            case 4:     // vmoveto
            case 22:    // hmoveto
                if (this.stack.size() > 1) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                this.stack.pop();
                break;
            case 21:    // rmoveto
                if (this.stack.size() > 2) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                this.popStack(2);
                break;
            case 1:     // hstem
            case 3:     // vstem
            case 18:    // hstemhm
            case 23:    // vstemhm
                if (this.stack.size() % 2 == 1) {
                    this.setWidth(this.stack.get(0));
                    return true;
                }
                this.stack.clear();
                break;
            case 28:    // actually not an operator but 2-byte number
                this.stack.push(readNextNumber(nextByte));
                break;
            case 10:    // subrcall
                int subrNum = (int) this.stack.pop().getInteger();
                if(this.stack.empty()) {
                    this.setWidth(getWidthFromSubroutine(localSubrs.get(subrNum + bias)));
                } else {
                    this.setWidth(this.stack.get(0));
                }
                return true;
            case 29:    // callgsubr
                subrNum = (int) this.stack.pop().getInteger();
                if(this.stack.empty()) {
                    this.setWidth(getWidthFromSubroutine(globalSubrs.get(subrNum + bias)));
                } else {
                    this.setWidth(this.stack.get(0));
                }return true;
            case 5:     // rlineto
            case 6:     // hlineto
            case 7:     // vlineto
            case 8:     // rrcurveto
            case 27:    // hhcurveto
            case 24:    // rcurveline
            case 25:    // rlinecurve
            case 26:    // vvcurveto
            case 30:    // vhcurveto
            case 31:    // hvcurveto
                this.stack.clear();     // this is perfectly correct handling of stack in case of these ops
                break;
            default:
                this.stack.clear();     // this is more of a hack. May not be fully correct, but correct enough
                break;
        }
        return false;
    }

    private CFFNumber getWidthFromSubroutine(byte[] subr) throws IOException {
        ASMemoryInStream subrStream = new ASMemoryInStream(subr, subr.length, false);
        Type2CharStringParser parser = new Type2CharStringParser(subrStream,
                this.localSubrs, this.bias, this.globalSubrs);
        return parser.getWidth();
    }

    @Override
    protected CFFNumber readNextNumber(int firstByte) throws IOException {
        byte[] buf = new byte[4];
        if (firstByte == 28) {
            this.stream.read(buf, 2);
            return new CFFNumber((char) (((buf[0] & 0xFF) << 8) | (buf[1] & 0xFF)));
        } else {
            this.stream.read(buf, 4);
            int integer = 0;
            for (int i = 0; i < 3; ++i) {
                integer |= (buf[i] & 0xFF);
                integer <<= 8;
            }
            integer |= buf[3] & 0xFF;
            float res = integer;
            return new CFFNumber(res / TWO_POWER_16);
        }
    }
}
