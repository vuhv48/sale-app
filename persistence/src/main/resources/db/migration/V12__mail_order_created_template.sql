-- Template thông báo đơn hàng mới (gửi async qua mail_queue + Kafka).
INSERT INTO mail_template (code, subject_template, body_html, body_text)
VALUES (
    'ORDER_CREATED',
    'Đơn hàng {{orderNo}} đã được tạo — {{appName}}',
    '<p>Xin chào <strong>{{customerName}}</strong>,</p>'
        || '<p>Đơn hàng <strong>{{orderNo}}</strong> (mã nội bộ: <code>{{orderId}}</code>) đã được tạo thành công tại <em>{{appName}}</em>.</p>'
        || '<p>Tổng tiền: <strong>{{totalAmount}}</strong></p>'
        || '<p>Cảm ơn bạn đã sử dụng dịch vụ.</p>',
    'Xin chao {{customerName}}, don hang {{orderNo}} da duoc tao. Tong tien: {{totalAmount}}. {{appName}}.'
);
