package worker

const TASK_DISPATCH_VERIFICATION_EMAIL = "task:dispatch_verification_email"

type PayloadDispatchVerificationEmail struct {
	UserID int64 `json:"id"`
}
