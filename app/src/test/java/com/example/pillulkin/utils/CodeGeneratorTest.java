package com.example.pillulkin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class CodeGeneratorTest {
    @Test
    public void generateCode_returnsNonNull() {
        String code = CodeGenerator.generateCode();
        assertNotNull(code);
    }

    @Test
    public void generateCode_returnsCorrectLength() {
        String code = CodeGenerator.generateCode();
        assertEquals(6, code.length());
    }

    @Test
    public void generateCode_containsOnlyValidCharacters() {
        String code = CodeGenerator.generateCode();
        assertTrue(code.matches("[A-Z0-9]+"));
    }

    @Test
    public void generateCode_generatesUniqueCodes() {
        String code1 = CodeGenerator.generateCode();
        String code2 = CodeGenerator.generateCode();
        assertNotEquals(code1, code2);
    }
}
