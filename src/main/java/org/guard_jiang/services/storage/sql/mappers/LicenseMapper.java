package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.License;
import org.guard_jiang.services.storage.sql.records.LicenseRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by icand on 2017/8/26.
 */
public interface LicenseMapper {

    @Nonnull
    List<License> getLicensesOfUser(
            @Param("userId") @Nonnull String userId);

    @Nullable
    LicenseRecord getLicense(
            @Param("licenseId") long licenseId,
            @Param("forUpdate") boolean forUpdate);

    void createLicense(
            @Param("license") @Nonnull License license
    );

    void updateLicenseUsage(
            @Param("licenseId") long licenseId,
            @Param("numDefendersAdd") int numDefendersAdd,
            @Param("numSupportersAdd") int numSupportersAdd,
            @Param("numAdminsAdd") int numAdminsAdd
    );
}
