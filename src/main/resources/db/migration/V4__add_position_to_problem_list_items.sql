ALTER TABLE problem_list_items ADD COLUMN position INT DEFAULT 0;

UPDATE problem_list_items pli
SET position = sub.row_num
    FROM (
    SELECT problem_id, problem_list_id,
           ROW_NUMBER() OVER (
               PARTITION BY problem_list_id ORDER BY ctid
           ) AS row_num
    FROM problem_list_items
) sub
WHERE pli.problem_id = sub.problem_id AND pli.problem_list_id = sub.problem_list_id;