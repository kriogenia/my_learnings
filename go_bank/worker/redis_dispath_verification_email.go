package worker

import (
	"context"
	"database/sql"
	"encoding/json"
	"fmt"

	"github.com/hibiken/asynq"
	"github.com/rs/zerolog/log"
)

func (distributor *RedisTaskDistributor) DistributeDispatchVerificationEmailTask(
	ctx context.Context,
	payload *PayloadDispatchVerificationEmail,
	opts ...asynq.Option,
) error {
	jsonPayload, err := json.Marshal(payload)
	if err != nil {
		return fmt.Errorf("failed to marshal task payload: %w", err)
	}

	task := asynq.NewTask(TASK_DISPATCH_VERIFICATION_EMAIL, jsonPayload)

	taskInfo, err := distributor.client.EnqueueContext(ctx, task, opts...)
	if err != nil {
		return fmt.Errorf("failed to enqueue task: %w", err)
	}

	log.Info().
		Str("type", task.Type()).
		Bytes("payload", task.Payload()).
		Str("queue", taskInfo.Queue).
		Int("max_retry", taskInfo.MaxRetry).
		Msg("enqueued dispatch verification email task")

	return nil
}

func (processor *RedisTaskProcessor) ProcessDispatchVerificationEmailTask(ctx context.Context, task *asynq.Task) error {
	var payload PayloadDispatchVerificationEmail
	if err := json.Unmarshal(task.Payload(), &payload); err != nil {
		return fmt.Errorf("failed to unmarshal payload: %w", asynq.SkipRetry)
	}

	user, err := processor.store.GetUser(ctx, payload.UserID)
	if err != nil {
		if err == sql.ErrNoRows {
			return fmt.Errorf("user %d not found: %w", payload.UserID, err)
		}
		return fmt.Errorf("failed to retrieve user: %w", err)
	}

	log.Info().
		Str("type", task.Type()).
		Bytes("payload", task.Payload()).
		Str("email", user.Email).
		Msg("processed dispatch verification email task")

	return nil
}
