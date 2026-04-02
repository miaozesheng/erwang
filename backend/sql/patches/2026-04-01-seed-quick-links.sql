USE blog;

INSERT INTO `quick_link` (`icon`, `label`, `url`, `sort`)
SELECT '🐙', 'GitHub', 'https://github.com', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `quick_link` WHERE `label` = 'GitHub' AND `deleted` = 0);

INSERT INTO `quick_link` (`icon`, `label`, `url`, `sort`)
SELECT '⚡', '掘金', 'https://juejin.cn', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `quick_link` WHERE `label` = '掘金' AND `deleted` = 0);

INSERT INTO `quick_link` (`icon`, `label`, `url`, `sort`)
SELECT '✍', '知乎', 'https://zhihu.com', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `quick_link` WHERE `label` = '知乎' AND `deleted` = 0);

SELECT `id`, `label`, `url`, `sort`
FROM `quick_link`
WHERE `deleted` = 0
ORDER BY `sort` ASC;
