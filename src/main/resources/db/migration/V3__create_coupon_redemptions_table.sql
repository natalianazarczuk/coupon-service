CREATE TABLE coupon_redemptions
(
    id          BIGSERIAL PRIMARY KEY,
    coupon_id   BIGINT      NOT NULL REFERENCES coupons (id),
    user_uuid   UUID        NOT NULL,
    redeemed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT unique_coupon_redemption UNIQUE (coupon_id, user_uuid)
);