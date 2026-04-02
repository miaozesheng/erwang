USE blog;

UPDATE `user`
SET
  `password` = '$2y$10$Uom/bv1SRMLiU8g31Vcr5ejwMMX/Ev9ETHzLBFAHz9wSKo/ZFWUl.',
  `role` = 'admin',
  `status` = 1,
  `deleted` = 0,
  `updated_at` = NOW()
WHERE `username` = 'admin';

SELECT `id`, `username`, `role`, `status`, `deleted`
FROM `user`
WHERE `username` = 'admin';
