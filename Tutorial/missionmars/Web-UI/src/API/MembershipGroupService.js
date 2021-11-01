import axios from "./axiosInstance";

const getMyMemberInfo = async () => {
  try {
    const response = await axios.get("membershipgroup/getmymemberinfo");
    return response;
  } catch (error) {
    throw error;
  }
};

const getAllMembers = async () => {
  try {
    const response = await axios.get("membershipgroup/getallmembers");
    return response;
  } catch (error) {
    throw error;
  }
};

const MembershipGroupService = {
  getMyMemberInfo,
  getAllMembers,
};

export default MembershipGroupService;
