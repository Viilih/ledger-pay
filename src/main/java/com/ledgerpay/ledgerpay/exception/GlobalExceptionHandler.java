package com.ledgerpay.ledgerpay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAccountStatusTransitionException.class)
    public ProblemDetail handleInvalidAccountStatusTransition(InvalidAccountStatusTransitionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Invalid account status transition");
        return problem;
    }

    @ExceptionHandler(AccountAlreadyExistException.class)
    public ProblemDetail handleAccountAlreadyExists(
            AccountAlreadyExistException exception
    ){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
        problem.setTitle("Account already exists");
        return problem;
    }

    @ExceptionHandler(AccountNotFoundWithIdException.class)
    public ProblemDetail handleAccountNotFound(
            AccountNotFoundWithIdException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
        problem.setTitle("Account not found");
        return problem;
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ProblemDetail handleInvalidTransaction(InvalidTransactionException exception){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problem.setTitle("Invalid transaction");
        return problem;
    }
}
