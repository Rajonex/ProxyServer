package App;

public class Statistics {
    private int questionsNumber;
    private long sendedData;
    private long receivedData;

    public void increaseQuestionNumber(int increase)
    {
        questionsNumber += increase;
    }

    public void increaseSendedData(long increase)
    {
        sendedData += increase;
    }

    public void increaseReceivedData(long increase)
    {
        receivedData += increase;
    }

    public Statistics(int questionsNumber, long sendedData, long receivedData) {
        this.questionsNumber = questionsNumber;
        this.sendedData = sendedData;
        this.receivedData = receivedData;
    }

    public int getQuestionsNumber() {
        return questionsNumber;
    }

    public void setQuestionsNumber(int questionsNumber) {
        this.questionsNumber = questionsNumber;
    }

    public long getSendedData() {
        return sendedData;
    }

    public void setSendedData(long sendedData) {
        this.sendedData = sendedData;
    }

    public long getReceivedData() {
        return receivedData;
    }

    public void setReceivedData(long receivedData) {
        this.receivedData = receivedData;
    }
}
