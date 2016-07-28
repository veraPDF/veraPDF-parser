package org.verapdf.font.type1;

import org.verapdf.as.io.ASInputStream;

import java.io.IOException;
import java.util.Stack;

/**
 * This class parses charstring data in font Type 1 files after it was
 * eexec-decoded. In particular, it extracts glyph width info.
 *
 * @author Sergey Shemyakov
 */
public class CharStringParser {

    private ASInputStream decodedCharString;
    private Stack<Integer> type1BuildCharOperandStack;
    private int width;

    /**
     * Constructor from stream and Type 1 Build Char stack.
     *
     * @param decodedCharString is stream with decoded charstring.
     */
    CharStringParser(ASInputStream decodedCharString) throws IOException {
        this.decodedCharString = decodedCharString;
        this.type1BuildCharOperandStack = new Stack<>();
        parse();
    }

    /**
     * Parses this charstring in order to extract width value and construct Type
     * 1 Build Char stack correctly.
     *
     * @throws IOException if stream reading error occurs.
     */
    private void parse() throws IOException {
        byte[] buf = new byte[1];
        int cont = this.decodedCharString.read(buf, 1);
        while (cont != -1) {
            int nextByte = buf[0] & 0xFF;
            if (nextByte > 31) {
                this.type1BuildCharOperandStack.push((int) getNextInteger(nextByte));
            } else {
                if (processNextOperator(nextByte)) {
                    return;
                }
            }
            cont = this.decodedCharString.read(buf, 1);
        }
    }

    /**
     * @return width of this character as specified in operator hsbw or sbw.
     */
    int getWidth() {
        return width;
    }

    private long getNextInteger(int firstByte) throws IOException {
        byte[] buf = new byte[1];
        if (firstByte > 31 && firstByte < 247) {
            return firstByte - 139;
        } else if (firstByte > 246 && firstByte < 251) {
            this.decodedCharString.read(buf, 1);
            return ((firstByte - 247) << 8) + (buf[0] & 0xFF) + 108;
        } else if (firstByte > 250 && firstByte < 255) {
            this.decodedCharString.read(buf, 1);
            return -((firstByte - 251) << 8) - (buf[0] & 0xFF) - 108;
        } else {
            long res = 0;
            for (int i = 0; i < 4; ++i) {
                this.decodedCharString.read(buf, 1);
                res |= (buf[0] & 0xFF);
                res <<= 8;
            }
            res >>= 8;
            return res;
        }
    }

    /**
     * Processes next bytes in stream if it is an operator.
     *
     * @param firstByte is first byte of operator.
     * @return true if width was extracted from this operator.
     * @throws IOException if stream reading error occurs.
     */
    private boolean processNextOperator(int firstByte) throws IOException {
        if (firstByte != 12) {
            switch (firstByte) {
                case 14:    // endchar
                case 9:     // closepath
                case 11:    // return
                    break;
                case 6:     // hlineto
                case 22:    // hmoveto
                case 7:     // vlineto
                case 4:     // vmoveto
                    popStack(1);
                    break;
                case 5:     // rlineto
                case 21:    // rmoveto
                case 1:     // hstem
                case 3:     // vstem
                    popStack(2);
                    break;
                case 31:    // hvcurveto
                case 30:    // vhcurveto
                    popStack(4);
                    break;
                case 8:     // rrcurveto
                    popStack(6);
                    break;
                case 13:    //hsbw
                    this.width = this.type1BuildCharOperandStack.pop();
                    popStack(1);
                    return true;
                case 10:    // callsubr
                    //TODO: should we parse this?
                    break;
            }
        } else {
            byte[] buf = new byte[1];
            this.decodedCharString.read(buf, 1);
            switch (buf[0] & 0xFF) {
                case 0:     // dotsection
                    break;
                case 33:    // setcurrentpoint
                    popStack(2);
                    break;
                case 6:     // seac
                    popStack(5);
                    break;
                case 2:     // hstem3
                case 1:     // vstem3
                    popStack(6);
                    break;
                case 16:    // callothersubr
                case 17:    // pop
                    //TODO: should we parse this?
                    break;
                case 7:     // sbw
                    popStack(1);
                    this.width = this.type1BuildCharOperandStack.pop();
                    popStack(2);
                    return true;
                case 12:    // div
                    int num2 = this.type1BuildCharOperandStack.pop();
                    int num1 = this.type1BuildCharOperandStack.pop();
                    this.type1BuildCharOperandStack.push(num1 / num2);  // That is not exactly what we should do, pushed number should be real. But we know that
                    break;  // width is integer, so the result of division is not needed.
            }
        }
        return false;
    }

    /**
     * Pops several operands from Type 1 Build Char stack.
     *
     * @param num is amount of numbers to be popped.
     * @throws IOException if stream reading error occurs.
     */
    private void popStack(int num) throws IOException {
        for (int i = 0; i < num && !this.type1BuildCharOperandStack.empty(); ++i) {
            this.type1BuildCharOperandStack.pop();
        }
    }

}
