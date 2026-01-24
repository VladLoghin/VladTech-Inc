import { api } from "./http";

export async function uploadEmployeeProjectPhoto(projectIdentifier, token, file) {
  const form = new FormData();
  form.append("photo", file);

  const res = await api.post(
    `/employee/projects/${projectIdentifier}/photo`,
    form,
    {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return res.data;
}
