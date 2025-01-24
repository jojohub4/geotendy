package com.example.geotendy;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class CourseUnit implements Parcelable {
    @SerializedName("unit_code")
    private String unitCode;

    @SerializedName("unit_name")
    private String unitName;

    public CourseUnit(String unitCode, String unitName) {
        this.unitCode = unitCode;
        this.unitName = unitName;
    }

    protected CourseUnit(Parcel in) {
        unitCode = in.readString();
        unitName = in.readString();
    }

    public static final Creator<CourseUnit> CREATOR = new Creator<CourseUnit>() {
        @Override
        public CourseUnit createFromParcel(Parcel in) {
            return new CourseUnit(in);
        }

        @Override
        public CourseUnit[] newArray(int size) {
            return new CourseUnit[size];
        }
    };

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(unitCode);
        dest.writeString(unitName);
    }
}
