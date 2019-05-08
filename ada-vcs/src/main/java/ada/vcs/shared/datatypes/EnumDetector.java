package ada.vcs.shared.datatypes;

import ada.commons.util.Either;
import ada.commons.util.NameFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(staticName = "apply", access = AccessLevel.PRIVATE)
public final class EnumDetector implements DataTypeDetector<EnumDetector> {

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
    public Proximity getProximity() {
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
    public EnumDetector withOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }

    public EnumDetector withSymbols(List<String> symbols) {
        this.symbols = symbols;
        return this;
    }

    @Override
    public Field type(String fieldName) {
        EnumTypeHelper h = EnumTypeHelper.apply(fieldName, isOptional, Optional.ofNullable(symbols).orElse(Lists.newArrayList()), nf);
        String values = String.join(", ", h.symbols);

        return DetectedField.apply(fieldName, "Enumeration", h, h, String.format("Values: %s", values));
    }

    public static Either<Object, Exception> parse(String value, List<String> symbols, NameFactory nf) {
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

    public static Either<Object, Exception> parse(String value, List<String> symbols, NameFactory nf, Schema schema) {
        try {
            value = nf.create(value.trim());
        } catch (Exception e) {
            return Either.right(e);
        }

        if (symbols.contains(value)) {
            return Either.left(new GenericData.EnumSymbol(schema, value));
        } else {
            String message = String.format("'%s' is not a correct value for this enumeration", value);
            return Either.right(new IllegalArgumentException(message));
        }
    }

    private static boolean isNullValue(String value) {
        return value == null || value.trim().length() == 0;
    }

    @AllArgsConstructor(staticName = "apply")
    private static class EnumTypeHelper implements DetectedField.Builder, DetectedField.Parser {

        private final String fieldName;

        private final boolean isOptional;

        private final ImmutableList<String> symbols;

        private final NameFactory nf;

        private final Schema schema;

        static EnumTypeHelper apply(String fieldName, boolean isOptional, List<String> symbols, NameFactory nf) {
            String[] s = new String[symbols.size()];
            symbols.toArray(s);

            Schema schema = SchemaBuilder
                .enumeration(fieldName)
                .symbols(s);

            return apply(fieldName, isOptional, ImmutableList.copyOf(symbols), nf, schema);
        }

        @Override
        public Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.FieldAssembler<Schema>> build() {


            Function<SchemaBuilder.FieldAssembler<Schema>, SchemaBuilder.GenericDefault<Schema>> enumField = builder -> builder
                .name(fieldName)
                .type(schema);

            if (isOptional) {
                return builder -> enumField.apply(builder).withDefault(null);
            } else {
                return builder -> enumField.apply(builder).noDefault();
            }
        }

        @Override
        public Either<Object, Exception> parse(String value) {
            boolean isNull = isNullValue(value);

            if (isNull && isOptional) {
                return Either.left(null);
            } else if (isNull) {
                return Either.right(DetectedField.NotOptionalException.apply(fieldName));
            } else {
                return EnumDetector.parse(value, symbols, nf, schema);
            }
        }

    }

}
