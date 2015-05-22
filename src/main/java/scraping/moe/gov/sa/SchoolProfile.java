package scraping.moe.gov.sa;

/**
 * User: YamStranger
 * Date: 4/30/15
 * Time: 2:41 PM
 */

/**
 * bad class
 */
public class SchoolProfile {
    String id = ""; //School ID
    String ministryNumber = ""; //School Ministry Number
    String name = "";//School Name
    String established = "";//Year Established
    String classification = "";//School Classification
    String office = "";//Education Office
    String district = "";//District Name
    String region = "";//Administrative Region
    String country = "";//Administrative Contry
    String centers = "";//Administrative Centers
    String address = "";//School Address
    String email = "";//School Email
    String phone = "";//School Phone
    String fax = "";//School Fax
    String level = "";//Study Level
    String gender = "";//School Gender
    String students = "";//Student Count
    String saudiStudents = "";//Saudi Student Count
    String notSaudiStudents = "";//NonSaudi Student Count
    String teachers = "";//Teachers Count
    String saudiTeachers = "";//Saudi Teacher Count
    String notSaudiTeachers = "";//NonSaudi Teacher Count
    String classes = "";//Classes Count

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchoolProfile that = (SchoolProfile) o;

        if (address != null ? !address.equals(that.address) : that.address != null)
            return false;
        if (centers != null ? !centers.equals(that.centers) : that.centers != null)
            return false;
        if (classes != null ? !classes.equals(that.classes) : that.classes != null)
            return false;
        if (classification != null ? !classification.equals(that.classification) : that.classification != null)
            return false;
        if (country != null ? !country.equals(that.country) : that.country != null)
            return false;
        if (district != null ? !district.equals(that.district) : that.district != null)
            return false;
        if (email != null ? !email.equals(that.email) : that.email != null)
            return false;
        if (established != null ? !established.equals(that.established) : that.established != null)
            return false;
        if (fax != null ? !fax.equals(that.fax) : that.fax != null)
            return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (level != null ? !level.equals(that.level) : that.level != null)
            return false;
        if (ministryNumber != null ? !ministryNumber.equals(that.ministryNumber) : that.ministryNumber != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (notSaudiStudents != null ? !notSaudiStudents.equals(that.notSaudiStudents) : that.notSaudiStudents != null)
            return false;
        if (notSaudiTeachers != null ? !notSaudiTeachers.equals(that.notSaudiTeachers) : that.notSaudiTeachers != null)
            return false;
        if (office != null ? !office.equals(that.office) : that.office != null)
            return false;
        if (phone != null ? !phone.equals(that.phone) : that.phone != null)
            return false;
        if (region != null ? !region.equals(that.region) : that.region != null)
            return false;
        if (saudiStudents != null ? !saudiStudents.equals(that.saudiStudents) : that.saudiStudents != null)
            return false;
        if (saudiTeachers != null ? !saudiTeachers.equals(that.saudiTeachers) : that.saudiTeachers != null)
            return false;
        if (students != null ? !students.equals(that.students) : that.students != null)
            return false;
        if (teachers != null ? !teachers.equals(that.teachers) : that.teachers != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ministryNumber != null ? ministryNumber.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (established != null ? established.hashCode() : 0);
        result = 31 * result + (classification != null ? classification.hashCode() : 0);
        result = 31 * result + (office != null ? office.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (centers != null ? centers.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (students != null ? students.hashCode() : 0);
        result = 31 * result + (saudiStudents != null ? saudiStudents.hashCode() : 0);
        result = 31 * result + (notSaudiStudents != null ? notSaudiStudents.hashCode() : 0);
        result = 31 * result + (teachers != null ? teachers.hashCode() : 0);
        result = 31 * result + (saudiTeachers != null ? saudiTeachers.hashCode() : 0);
        result = 31 * result + (notSaudiTeachers != null ? notSaudiTeachers.hashCode() : 0);
        result = 31 * result + (classes != null ? classes.hashCode() : 0);
        return result;
    }
}
