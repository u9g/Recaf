package me.coley.recaf.parse.bytecode;

import me.coley.recaf.parse.bytecode.exception.VerifierException;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

/**
 * Basic method analyzer.
 *
 * @author Matt
 */
class Verifier extends RAnalyzer {
	private final String currentType;
	private final MethodAssembler assembler;

	/**
	 * Create the verifier.
	 *
	 * @param assembler
	 * 		Assembler responsible assembling the method.
	 * @param currentType
	 * 		Declaring type of method to be analyzed.
	 */
	Verifier(MethodAssembler assembler, String currentType) {
		super(new RInterpreter());
		this.currentType = currentType;
		this.assembler = assembler;
	}

	Frame<RValue>[] verify(MethodNode method) throws VerifierException {
		try {
			return analyze(currentType, method);
		} catch(AnalyzerException ex) {
			// Thrown on verify failure.
			int line = assembler.getLine(ex.node);
			throw new VerifierException(ex,
					"Verification failed on line: " + line + "\n" + ex.getMessage(), line);
		} catch(Exception ex) {
			// IndexOutOfBoundsException: When local variables are messed up
			// Exception: ?
			throw new VerifierException(ex, "Verifier crashed: (" + ex.getClass().getSimpleName() + ") " + ex.getMessage(), -1);
		}
	}
}
