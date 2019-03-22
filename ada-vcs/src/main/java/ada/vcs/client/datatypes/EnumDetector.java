package ada.vcs.client.datatypes;

import ada.commons.Either;
import ada.commons.NameFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class EnumDetector implements DataTypeDetector {

    private final int maxSymbols;

    private final Proximity proximity;

    private final NameFactory nf;

    private boolean isOptional;

    private List<String> symbols;

    public static EnumDetector apply(int maxSymbols) {
        return apply(maxSymbols, Proximity.apply(), NameFactory.apply(NameFactory.Defaults.LOWERCASE_UNDERSCORED), false, Lists.newArrayList());
    }

    public static EnumDetector apply() {
        return apply(10);
    }

    @Override
    public Proximity proximity() {
        return proximity;
    }

    @Override
    public void hint(String value) {
        if (isNullValue(value)) {
            isOptional = true;
            proximity.countNullValue();
            return;
        }

        try {
            value = nf.create(value.trim());
        } catch (NameFactory.InvalidNameException e) {
            proximity.countInvalidValue();
            return;
        }

        if (symbols.contains(value)) {
            proximity.countCorrectValue();
        } else if (symbols.size() < maxSymbols) {
            proximity.countCorrectValue();
            symbols.add(value);
        } else {
            proximity.countInvalidValue();
        }
    }

    @Override
    public DataType type() {
        EnumTypeHelper h = EnumTypeHelper.apply(isOptional, Optional.ofNullable(symbols).orElse(Lists.newArrayList()), nf);
        String values = String.join(", ", h.symbols);

        return DetectedDataType.apply("Enumeration", h, h, String.format("Values: %s", values));
    }

    public static Either<Object, Exception> parse(String value, List<String> symbols, NameFactory nf) {
        if (isNullValue(value)) {
            return Either.left(null);
        } else {
            try {
                value = nf.create(value.trim());
            } catch (Exception e) {
                return Either.right(e);
            }

            if (symbols.contains(value)) {
                return Either.left(value);
            } else {
                String message = String.format("'%s' is not a correct value for this enumeration", value);
                return Either.right(new IllegalArgumentException(message));
            }
        }
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class EnumTypeHelper implements DetectedDataType.Builder, DetectedDataType.Parser {

        private final boolean isOptional;

        private final ImmutableList<String> symbols;

        private final NameFactory nf;

        static EnumTypeHelper apply(boolean isOptional, List<String> symbols, NameFactory nf) {
            return apply(isOptional, ImmutableList.copyOf(symbols), nf);
        }

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build(String fieldName) {
            String[] s = new String[symbols.size()];
            symbols.toArray(s);

            Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.EnumDefault<Schema>> enumField = builder -> builder
                .name(fieldName)
                .type()
                .enumeration(fieldName)
                .symbols(s);

            if (isOptional) {
                return builder -> enumField.apply(builder).enumDefault(null);
            } else {
                return builder -> enumField.apply(builder).noDefault();
            }
        }

        @Override
        public Either<Object, Exception> parse(String value) {
            return EnumDetector.parse(value, symbols, nf);
        }

    }

}
