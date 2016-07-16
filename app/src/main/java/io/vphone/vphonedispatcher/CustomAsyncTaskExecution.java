package io.vphone.vphonedispatcher;

/**
 * This interface (@code CustomAsyncTaskExecution) is used to supply asynctask with custom functionality.
 * @param <Result> Result type that will be processed
 */
public interface CustomAsyncTaskExecution<Result> {
    void preExecution();
    void postExecution(Result resultParam);
}
