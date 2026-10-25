package com.borrowbuddy.model;

import java.sql.Timestamp;

/** Records a participant's approval decision for a transaction. */
public class TransactionApproval {
    private int approvalId;
    private int transactionId;
    private int userId;
    private String decision;       // PENDING | ACCEPTED | REJECTED
    private Timestamp decisionDate;
    private String userName;       // transient

    public TransactionApproval() {}

    public int getApprovalId()         { return approvalId; }
    public int getTransactionId()      { return transactionId; }
    public int getUserId()             { return userId; }
    public String getDecision()        { return decision; }
    public Timestamp getDecisionDate() { return decisionDate; }
    public String getUserName()        { return userName; }

    public void setApprovalId(int id)           { this.approvalId = id; }
    public void setTransactionId(int id)        { this.transactionId = id; }
    public void setUserId(int id)               { this.userId = id; }
    public void setDecision(String decision)    { this.decision = decision; }
    public void setDecisionDate(Timestamp t)    { this.decisionDate = t; }
    public void setUserName(String name)        { this.userName = name; }

    public boolean isPending()  { return "PENDING".equals(decision); }
    public boolean isAccepted() { return "ACCEPTED".equals(decision); }
    public boolean isRejected() { return "REJECTED".equals(decision); }
}
