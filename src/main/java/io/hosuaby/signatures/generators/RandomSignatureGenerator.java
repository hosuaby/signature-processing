package io.hosuaby.signatures.generators;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.codearte.jfairy.Fairy;

/**
 * Generator of random human signature.
 */
@Component
public class RandomSignatureGenerator {

    /** Fairy object */
    @Autowired
    private Fairy fairy;

    /** Generator of random sign */
    @Autowired
    private RandomSignSvgGenerator signGenerator;

    /**
     * @return generated random signature
     */
    public RandomSignature randomSignature() {
        return new RandomSignature() {{
            setPerson(fairy.person());
            setSign(signGenerator.randomSign());
        }};
    }

    /**
     * Generates provided number of random signatures.
     * @param n    number of required signatures
     * @return list of random signatures
     */
    public List<RandomSignature> randomSignatures(int n) {
        List<RandomSignature> signatures = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            signatures.add(randomSignature());
        }

        return signatures;
    }

}
