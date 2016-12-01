package org.verapdf.pd.encryption;

/**
 * Represents access permissions for document.
 *
 * @author Sergey Shemyakov
 */
public class AccessPermissions {

    private static final int DEFAULT_PERMISSIONS = 0xFFFFFFFF;
    private static final int PRINT_BIT = 3;
    private static final int MODIFY_BIT = 4;
    private static final int EXTRACT_BIT = 5;
    private static final int MODIFY_ANNOTATIONS_BIT = 6;
    private static final int FILL_IN_BIT = 9;
    private static final int EXTRACT_FOR_ACCESSIBILITY_BIT = 10;
    private static final int ASSEMBLE_DOCUMENT_BIT = 11;
    private static final int DEGRADED_PRINT_BIT = 12;

    private int bits;

    private AccessPermissions(int bits) {
        this.bits = bits;
    }

    /**
     * @return owner permissions where everything is permitted.
     */
    public static AccessPermissions getOwnerPermissions() {
        return new AccessPermissions(DEFAULT_PERMISSIONS);
    }

    /**
     * @param p is P value in encryption dict for standard security handler.
     * @return user permissions specified by p as described in 7.6.3.2 "Standard
     * Encryption Dictionary" in PDF32000_2008.
     */
    public static AccessPermissions getUserPermissions(int p) {
        return new AccessPermissions(p);
    }

    private boolean isPermissionBitOn(int bit) {
        return (bits & (1 << (bit - 1))) != 0;
    }

    /**
     * @return true If supplied with the user password they are allowed to print.
     */
    public boolean canPrint() {
        return isPermissionBitOn(PRINT_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to
     * modify the document.
     */
    public boolean canModify() {
        return isPermissionBitOn(MODIFY_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to
     * extract content from the PDF document.
     */
    public boolean canExtractContent() {
        return isPermissionBitOn(EXTRACT_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to
     * modify annotations.
     */
    public boolean canModifyAnnotations() {
        return isPermissionBitOn(MODIFY_ANNOTATIONS_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to fill
     * in form fields.
     */
    public boolean canFillInForm() {
        return isPermissionBitOn(FILL_IN_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to
     * extract content from the PDF document.
     */
    public boolean canExtractForAccessibility() {
        return isPermissionBitOn(EXTRACT_FOR_ACCESSIBILITY_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to
     * extract content from the PDF document.
     */
    public boolean canAssembleDocument() {
        return isPermissionBitOn(ASSEMBLE_DOCUMENT_BIT);
    }

    /**
     * @return true If supplied with the user password they are allowed to print
     * the document in a degraded format.
     */
    public boolean canPrintDegraded() {
        return isPermissionBitOn(DEGRADED_PRINT_BIT);
    }
}
