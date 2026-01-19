-- Link booking participants to saved participants for tracking
ALTER TABLE participants
ADD COLUMN saved_participant_id UUID REFERENCES user_saved_participants(id);
