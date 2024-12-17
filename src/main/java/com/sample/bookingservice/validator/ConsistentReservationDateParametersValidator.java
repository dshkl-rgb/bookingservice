package com.sample.bookingservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class ConsistentReservationDateParametersValidator implements
        ConstraintValidator<ConsistentReservationDateParameters, Object[]> {

    @Value("#{new Integer('${reservation.days.ahead.allowed}')}")
    private Integer allowedDaysAhead;

    @Value("#{new Integer('${reservation.duration.allowed}')}")
    private Integer allowedResevationDuration;

    @Override
    public void initialize(ConsistentReservationDateParameters constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object[] value, ConstraintValidatorContext context) {
        if (value.length < 2) {
            throw new IllegalArgumentException("Illegal method signature");
        }

        //leave null-checking to @NotNull on individual parameters
        if (value[0] == null || value[1] == null) {
            return true;
        }

        if (!(value[0] instanceof LocalDate) || !(value[1] instanceof LocalDate)) {
            throw new IllegalArgumentException(
                    "Illegal method signature, expected two " +
                            "parameters of type LocalDate."
            );
        }

        final LocalDate startDate = (LocalDate) value[0];
        final LocalDate endDate = (LocalDate) value[1];
        final LocalDate now = LocalDate.now();

        // startDate after endDate
        if (startDate.isAfter(endDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Start date cannot be after end date")
                    .addConstraintViolation();
            return false;
        }

        // duration of reservation longer that max
        if (startDate.plusDays(allowedResevationDuration).isBefore(endDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Reservation duration cannot be greater than:" + allowedResevationDuration + " days")
                    .addConstraintViolation();
            return false;
        }

        // endDate after max reservation days head
        if (endDate.isAfter(now.plusDays(allowedDaysAhead))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Reservation cannot be further ahead than: " + allowedDaysAhead + " days")
                    .addConstraintViolation();
            return false;
        }

        // startDate after max reservation days head
        if (startDate.isAfter(now.plusDays(allowedDaysAhead))) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Reservation cannot be further ahead than: " + allowedDaysAhead + " days")
                    .addConstraintViolation();
            return false;
        }

        if (startDate.isEqual(endDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Reservation needs to be at least 1 day")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
