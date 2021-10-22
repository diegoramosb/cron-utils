package com.cronutils.model;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronConstraint;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import com.cronutils.utils.Preconditions;

import java.math.BigInteger;
import java.util.*;

public class SingleCron implements Cron {
    private static final long serialVersionUID = 7487370826825439098L;
    private final CronDefinition cronDefinition;
    private final Map<CronFieldName, CronField> fields;
    private String asString;

    /**
     * Creates a Cron with the iven cron definition and the given fields.
     * @param cronDefinition the definition to use for this Cron
     * @param fields the fields that should be used
     */
    public SingleCron(final CronDefinition cronDefinition, final List<CronField> fields) {
        this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
        Preconditions.checkNotNull(fields, "CronFields cannot be null");
        this.fields = new EnumMap<>(CronFieldName.class);
        for (final CronField field : fields) {
            this.fields.put(field.getField(), field);
        }
    }

    /**
     * Retrieve value for cron field.
     *
     * @param name - cron field name.
     *             If null, a NullPointerException will be raised.
     * @return CronField that corresponds to given CronFieldName
     */
    public CronField retrieve(final CronFieldName name) {
        return fields.get(Preconditions.checkNotNull(name, "CronFieldName must not be null"));
    }

    /**
     * Retrieve all cron field values as map.
     *
     * @return unmodifiable Map with key CronFieldName and values CronField, never null
     */
    public Map<CronFieldName, CronField> retrieveFieldsAsMap() {
        return Collections.unmodifiableMap(fields);
    }

    public String asString() {
        if (asString == null) {
            final ArrayList<CronField> temporaryFields = new ArrayList<>(fields.values());
            temporaryFields.sort(CronField.createFieldComparator());
            final StringBuilder builder = new StringBuilder();
            for (final CronField field : temporaryFields) {
                builder.append(String.format("%s ", field.getExpression().asString()));
            }
            asString = builder.toString().trim();
        }
        return asString;
    }

    public CronDefinition getCronDefinition() {
        return cronDefinition;
    }

    /**
     * Validates this Cron instance by validating its cron expression.
     *
     * @return this Cron instance
     * @throws IllegalArgumentException if the cron expression is invalid
     */
    public Cron validate() {
        for (final Map.Entry<CronFieldName, CronField> field : retrieveFieldsAsMap().entrySet()) {
            final CronFieldName fieldName = field.getKey();
            field.getValue().getExpression().accept(
                    new ValidationFieldExpressionVisitor(getCronDefinition().getFieldDefinition(fieldName).getConstraints())
            );
        }
        for (final CronConstraint constraint : getCronDefinition().getCronConstraints()) {
            if (!constraint.validate(this)) {
                throw new IllegalArgumentException(String.format("Invalid cron expression: %s. %s", asString(), constraint.getDescription()));
            }
        }
        return this;
    }

    /**
     * Provides means to compare if two cron expressions are equivalent.
     *
     * @param cronMapper - maps 'cron' parameter to this instance definition;
     * @param cron       - any cron instance, never null
     * @return boolean - true if equivalent; false otherwise.
     */
    public boolean equivalent(final CronMapper cronMapper, final Cron cron) {
        return asString().equals(cronMapper.map(cron).asString());
    }

    /**
     * Provides means to compare if two cron expressions are equivalent.
     * Assumes same cron definition.
     *
     * @param cron - any cron instance, never null
     * @return boolean - true if equivalent; false otherwise.
     */
    public boolean equivalent(final Cron cron) {
        return asString().equals(cron.asString());
    }

    /**
     * Checks if the cron expression overlaps with another.
     *
     * @param cron - any cron instance, never null
     * @return boolean - true if the expression overlaps with another; false otherwise
     */
    @Override
    public boolean overlap(final Cron cron) {

        boolean overlap = false;

        return overlap;
    }

	public boolean yearsOverlap(final Cron cron) {

		final FieldExpression thisYear = this.retrieve(CronFieldName.YEAR).getExpression();
		final FieldExpression otherYear = cron.retrieve(CronFieldName.YEAR).getExpression();
		final Object thisYearClass = thisYear.getClass();
		final Object otherYearClass = otherYear.getClass();
		boolean overlap = false;

		if (Always.class.equals(thisYearClass) || Always.class.equals(otherYearClass)) {
			overlap = true;
		}
		else if (On.class.equals(thisYear.getClass())) {
			overlap = onAndOtherOverlap((On) thisYear, otherYear);
		}
		else if(Every.class.equals(thisYear.getClass())) {
			overlap = everyAndOtherOverlap((Every) thisYear, otherYear);
		}
//		else if(Between.class.equals(thisYear.getClass())) {
//			if(On.class.equals(otherYear.getClass())) {
//				overlap = yearsOnAndBetweenOverlap((On) otherYear, (Between) thisYear);
//			}
//		}
//		else if(And.class.equals(thisYear.getClass())) {
//			overlap = yearsOnAndOverlap((On) otherYear, (And) thisYear);
//		}
		return overlap;
	}

	private boolean onAndOtherOverlap(final On onYear, final FieldExpression otherYear) {
		final BigInteger onYearValue = BigInteger.valueOf(onYear.getTime().getValue());
		final BigInteger onYearPeriod = BigInteger.ZERO;
		BigInteger otherYearValue = null;
		BigInteger otherYearPeriod = null;
		if (On.class.equals(otherYear.getClass())) {
			otherYearValue = BigInteger.valueOf(((On) otherYear).getTime().getValue());
			otherYearPeriod = BigInteger.ZERO;
			return seriesOverlap(onYearValue, onYearPeriod, otherYearValue, otherYearPeriod);
		}
		else if (Every.class.equals(otherYear.getClass())) {
			otherYearValue = BigInteger.valueOf(((On) ((Every) otherYear).getExpression()).getTime().getValue());
			otherYearPeriod = BigInteger.valueOf(((Every) otherYear).getPeriod().getValue());
			return onYearValue.compareTo(otherYearValue) >= 0 && seriesOverlap(onYearValue, onYearPeriod, otherYearValue,
																			   otherYearPeriod);
		}
		else if (Between.class.equals(otherYear.getClass())) {
			final Integer startYear = (Integer) ((Between) otherYear).getFrom().getValue();
			final Integer endYear = (Integer) ((Between) otherYear).getTo().getValue();
			boolean overlap = false;
			for(int i = startYear; i <= endYear && !overlap; i++ ) {
				overlap = seriesOverlap(onYearValue, onYearPeriod, BigInteger.valueOf(startYear), BigInteger.ZERO);
			}
			return overlap;
		}
		else if (And.class.equals(otherYear.getClass())) {
			return ((And) otherYear).getExpressions().stream()
							 .anyMatch(year ->
											   seriesOverlap(onYearValue, onYearPeriod,
															 BigInteger.valueOf(((On) year).getTime().getValue()), BigInteger.ZERO));
		}
		return false;
	}

	private boolean everyAndOtherOverlap(final Every everyYearExpression, final FieldExpression otherYearExpression) {
		final BigInteger thisYearValue = BigInteger.valueOf(((On) everyYearExpression.getExpression()).getTime().getValue());
		final BigInteger thisYearPeriod = BigInteger.valueOf((everyYearExpression).getPeriod().getValue());
		if(On.class.equals(otherYearExpression.getClass())) {
			final BigInteger otherYearValue = BigInteger.valueOf(((On) otherYearExpression).getTime().getValue());
			final BigInteger otherYearPeriod = BigInteger.ZERO;
			return seriesOverlap(thisYearValue, thisYearPeriod, otherYearValue, otherYearPeriod);
		}
		else if(Every.class.equals(otherYearExpression.getClass())) {
			final BigInteger otherYearValue = BigInteger.valueOf(((On) ((Every) otherYearExpression).getExpression()).getTime().getValue());
			final BigInteger otherYearPeriod = BigInteger.valueOf(((Every) otherYearExpression).getPeriod().getValue());
			return seriesOverlap(thisYearValue, thisYearPeriod, otherYearValue, otherYearPeriod);
		}
		else if(Between.class.equals(otherYearExpression.getClass())) {
			final Integer startYear = (Integer) ((Between) otherYearExpression).getFrom().getValue();
			final Integer endYear = (Integer) ((Between) otherYearExpression).getTo().getValue();
			boolean overlap = false;
			for(int i = startYear; i <= endYear && !overlap; i++ ) {
				overlap = seriesOverlap(thisYearValue, thisYearPeriod, BigInteger.valueOf(startYear), BigInteger.ZERO);
			}
			return overlap;
		}
		else if (And.class.equals(otherYearExpression.getClass())) {
			return ((And) otherYearExpression).getExpressions().stream()
									.anyMatch(year ->
													  seriesOverlap(thisYearValue, thisYearPeriod,
																	BigInteger.valueOf(((On) year).getTime().getValue()), BigInteger.ZERO));
		}
		return false;
	}

	public boolean yearsOnAndOverlap(final On onYearExpression, final And andYearExpression) {
		return andYearExpression.getExpressions().stream()
								.anyMatch(year -> ((On) year).getTime().getValue().equals((onYearExpression).getTime().getValue()));
	}

	public boolean yearsOnAndBetweenOverlap(final On onYearExpression, final Between betweenYearsExpression) {

		return (onYearExpression).getTime().getValue() >= (Integer) (betweenYearsExpression).getFrom().getValue()
				|| (onYearExpression).getTime().getValue() <= (Integer) (betweenYearsExpression).getTo().getValue();
	}

	//https://math.stackexchange.com/questions/1656120/formula-to-find-the-first-intersection-of-two-arithmetic-progressions
	public boolean seriesOverlap(final BigInteger startYearA, final BigInteger incrementA, final BigInteger startYearB, final BigInteger incrementB) {

		boolean overlap = false;
		final BigInteger greatestCommonDivisor = incrementA.gcd(incrementB);
		final BigInteger startYearSubtraction = startYearA.subtract(startYearB);

		if(!greatestCommonDivisor.equals(BigInteger.ZERO)) {
			final BigInteger modulo = startYearSubtraction.mod(greatestCommonDivisor);
			overlap = modulo.equals(BigInteger.ZERO);
		} else {
			overlap = startYearSubtraction.equals(BigInteger.ZERO);
		}

		return overlap;
	}
}
