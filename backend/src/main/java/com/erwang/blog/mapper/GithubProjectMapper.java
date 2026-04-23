package com.erwang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erwang.blog.entity.GithubProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface GithubProjectMapper extends BaseMapper<GithubProject> {

    @Select({
            "<script>",
            "SELECT current.*",
            "FROM github_project current",
            "INNER JOIN (",
            "    SELECT repo_name, MAX(fetch_date) AS max_fetch_date",
            "    FROM github_project",
            "    WHERE deleted = 0 AND primary_category = 'all_time'",
            "    GROUP BY repo_name",
            ") latest ON current.repo_name = latest.repo_name",
            "        AND current.fetch_date = latest.max_fetch_date",
            "WHERE current.deleted = 0 AND current.primary_category = 'all_time'",
            "<if test='language != null and language != \"\"'>",
            "    AND current.language = #{language}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "    AND (current.repo_name LIKE CONCAT('%', #{keyword}, '%')",
            "      OR current.description LIKE CONCAT('%', #{keyword}, '%')",
            "      OR current.owner_name LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY current.stars DESC, current.forks DESC, current.fetch_date DESC",
            "</script>"
    })
    IPage<GithubProject> selectTotalStarLeaderboard(Page<GithubProject> page,
                                                    @Param("language") String language,
                                                    @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT current.*,",
            "       GREATEST(current.stars - COALESCE(previous.stars, current.stars), 0) AS star_growth",
            "FROM github_project current",
            "INNER JOIN (",
            "    SELECT repo_name, MAX(fetch_date) AS max_fetch_date",
            "    FROM github_project",
            "    WHERE deleted = 0 AND primary_category = 'all_time'",
            "    GROUP BY repo_name",
            ") latest ON current.repo_name = latest.repo_name",
            "        AND current.fetch_date = latest.max_fetch_date",
            "LEFT JOIN github_project previous ON previous.id = (",
            "    SELECT gp2.id",
            "    FROM github_project gp2",
            "    WHERE gp2.deleted = 0",
            "      AND gp2.primary_category = 'all_time'",
            "      AND gp2.repo_name = current.repo_name",
            "      AND gp2.fetch_date &lt;= #{cutoffDate}",
            "    ORDER BY gp2.fetch_date DESC",
            "    LIMIT 1",
            ")",
            "WHERE current.deleted = 0 AND current.primary_category = 'all_time'",
            "<if test='language != null and language != \"\"'>",
            "    AND current.language = #{language}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "    AND (current.repo_name LIKE CONCAT('%', #{keyword}, '%')",
            "      OR current.description LIKE CONCAT('%', #{keyword}, '%')",
            "      OR current.owner_name LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY star_growth DESC, current.stars DESC, current.forks DESC, current.fetch_date DESC",
            "</script>"
    })
    IPage<GithubProject> selectGrowthLeaderboard(Page<GithubProject> page,
                                                 @Param("cutoffDate") LocalDate cutoffDate,
                                                 @Param("language") String language,
                                                 @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT DISTINCT current.language",
            "FROM github_project current",
            "INNER JOIN (",
            "    SELECT repo_name, MAX(fetch_date) AS max_fetch_date",
            "    FROM github_project",
            "    WHERE deleted = 0 AND primary_category = 'all_time'",
            "    GROUP BY repo_name",
            ") latest ON current.repo_name = latest.repo_name",
            "        AND current.fetch_date = latest.max_fetch_date",
            "WHERE current.deleted = 0",
            "  AND current.primary_category = 'all_time'",
            "  AND current.language IS NOT NULL",
            "  AND current.language != ''",
            "ORDER BY current.language ASC",
            "</script>"
    })
    List<String> selectAvailableLanguagesFromLatestRecords();

    @Select("SELECT MAX(fetch_date) FROM github_project WHERE deleted = 0 AND primary_category = 'all_time'")
    LocalDate selectLatestFetchDate();
}
